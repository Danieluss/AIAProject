package me.danieluss.tournaments.controller;

import me.danieluss.tournaments.data.Specs;
import me.danieluss.tournaments.data.dto.*;
import me.danieluss.tournaments.data.model.AppUser;
import me.danieluss.tournaments.data.model.Match;
import me.danieluss.tournaments.data.model.Tournament;
import me.danieluss.tournaments.data.model.TournamentAppUser;
import me.danieluss.tournaments.data.repo.*;
import me.danieluss.tournaments.service.ScheduleService;
import me.danieluss.tournaments.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class TournamentController {

    private final Specs<Tournament> tournamentSpecs;
    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentAppUserRepository tournamentAppUserRepository;
    private final MatchRepository matchRepository;
    private final ScheduleService scheduleService;
    private final TournamentService tournamentService;

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return "";
    }

    @Autowired
    public TournamentController(Specs<Tournament> tournamentSpecs, UserRepository userRepository, TournamentRepository tournamentRepository, TournamentAppUserRepository tournamentAppUserRepository, MatchRepository matchRepository, ScheduleService scheduleService, TournamentService tournamentService) {
        this.tournamentSpecs = tournamentSpecs;
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
        this.tournamentAppUserRepository = tournamentAppUserRepository;
        this.matchRepository = matchRepository;
        this.scheduleService = scheduleService;
        this.tournamentService = tournamentService;
    }

    @RequestMapping(value = {"/list", "/home", "/"}, method = RequestMethod.GET)
    public String list(Model model,
                       @RequestParam(defaultValue = "") String search,
                       Pageable pageable) {
        Specification<Tournament> spec = tournamentSpecs.search(search);
        Page<Tournament> tournaments = tournamentRepository.findAll(spec, pageable);
        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("totalPages", tournaments.getTotalPages());
        model.addAttribute("tournaments", tournaments);
        model.addAttribute("search", search);
        return "list";
    }

    @RequestMapping(value = "/tournaments", method = RequestMethod.GET)
    public String userList(Model model,
                           Pageable pageable,
                           Principal principal) {
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getName());
        List<Tournament> tournaments = appUser.getParticipatedTournaments()
                .stream()
                .map(TournamentAppUser::getTournament)
                .filter(tournament -> tournament.getTime().after(new Date()))
                .collect(Collectors.toList());
        model.addAttribute("currentPage", pageable.getPageNumber() + 1);
        model.addAttribute("totalPages", 1);
        model.addAttribute("tournaments", tournaments);
        return "userList";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String edit(Model model, @RequestParam Long id, Principal principal) {
        Tournament tournament = tournamentRepository.getOne(id);
        if (!principal.getName().equalsIgnoreCase(tournament.getOrganizer().getEmail())) {
            model.addAttribute("toastMsg", "You are not allowed to edit this tournament.");
            return "list";
        }
        TournamentDTO tournamentDTO = new TournamentDTO(tournament);
        model.addAttribute("tournament", tournamentDTO);
        model.addAttribute("format", "yyyy-MM-dd'T'HH:mm");
        model.addAttribute("formTitle", "Edit Tournament");
        return "add";
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String upsert(Model model) {
        if (!model.containsAttribute("tournament")) {
            TournamentDTO tournament = new TournamentDTO();
            model.addAttribute("tournament", tournament);
        }
        model.addAttribute("formTitle", "Add Tournament");
        model.addAttribute("format", "yyyy-MM-dd'T'HH:mm");
        return "add";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String upsertTournament(Model model, RedirectAttributes redirectAttrs, @RequestParam(defaultValue = "-1") Long id, @Valid TournamentDTO tournamentDTO, BindingResult bindingResult, Principal principal) {
        if (tournamentDTO.getApplicationDeadline() == null || tournamentDTO.getTime() == null) {
            bindingResult.rejectValue("applicationDeadline", "error.tournament", "Dates are empty!");
        } else {
            if (tournamentDTO.getApplicationDeadline().after(tournamentDTO.getTime())) {
                bindingResult.rejectValue("applicationDeadline", "error.tournament", "Application deadline is after tournament date!");
            }
//            for demo purposes
//            if (tournamentDTO.getApplicationDeadline().before(new Date())) {
//                bindingResult.rejectValue("applicationDeadline", "error.tournament", "Application deadline is not future!");
//            }
//            if (tournamentDTO.getTime().before(new Date())) {
//                bindingResult.rejectValue("time", "error.tournament", "Tournament time is not future!");
//            }
        }

        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tournament", bindingResult);
            redirectAttrs.addFlashAttribute("tournament", tournamentDTO);
            return "redirect:add";
        }
// TODO add image validation (XSS)
        AppUser organizer = userRepository.findByEmailIgnoreCase(principal.getName());
        Tournament tournament = new Tournament(tournamentDTO, organizer);
        Tournament existingTournament = null;
        if (id != -1) {
            existingTournament = tournamentRepository.findById(id).orElse(null);
            if (existingTournament != null && existingTournament.getOrganizer().getEmail().equalsIgnoreCase(organizer.getEmail())) {
                if (tournamentDTO.getMaxParticipants() < existingTournament.getParticipants().size()) {
                    bindingResult.rejectValue("maxParticipants", "error.tournament", "You can't kick out participants like that!");
                    redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tournament", bindingResult);
                    redirectAttrs.addFlashAttribute("tournament", tournamentDTO);
                    return "redirect:add";
                }
                tournament.setId(id);
                tournamentService.save(tournament, existingTournament);
                scheduleService.schedule(tournament);
            } else {
                redirectAttrs.addFlashAttribute("toastMsg", "You're not the organizer of this tournament.");
                return "redirect:list";
            }
            redirectAttrs.addFlashAttribute("toastMsg", "Tournament edited.");
        } else {
            tournamentService.save(tournament, existingTournament);
            scheduleService.schedule(tournament);
            redirectAttrs.addFlashAttribute("toastMsg", "Tournament added.");
        }
        return "redirect:list";
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public String details(Model model, @RequestParam Long id, Principal principal) {
        Tournament tournament;
        if (!model.containsAttribute("tournament")) {
            tournament = tournamentRepository.getOne(id);
            model.addAttribute("tournament", tournament);
        } else {
            tournament = (Tournament) model.getAttribute("tournament");
        }
        assert tournament != null;
        boolean canApply = true;
        boolean canEdit = false;
        if (principal != null) {
            AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getName());
            canApply = tournamentService.canApply(tournament, appUser);
            canEdit = !principal.getName().equals("anonymousUser") && principal.getName().equalsIgnoreCase(tournament.getOrganizer().getEmail());
        }
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("organizer", tournament.getOrganizer());
        model.addAttribute("imgNos", IntStream.range(0, tournament.getNoImages()).boxed().collect(Collectors.toList()));
        model.addAttribute("canApply", canApply);

        if (tournament.getLadder() != null) {
            List<Integer> rounds = new ArrayList<>();
            List<List<MatchId>> roundMatches = new ArrayList<>();
            List<Match> matches = tournament.getLadder().getMatches();
            matches.sort(Comparator.comparingLong(Match::getId));
            int totalMatches = matches.size();
            int last = 0;
            for (int i = (totalMatches + 1) >> 1; i >= 1; i >>= 1) {
                List<MatchId> nextRound = new ArrayList<>();
                for (int k = last; k < last + i; k++) {
                    MatchId matchId = new MatchId();
                    matchId.setMatch(matches.get(k));
                    matchId.setId(k);
                    nextRound.add(matchId);
                }
                rounds.add(i);
                last += i;
                roundMatches.add(nextRound);
            }
            model.addAttribute("roundMatches", roundMatches);
            model.addAttribute("rounds", rounds);
            model.addAttribute("canApply", canApply);
        }
        model.addAttribute("winInfo", new WinInfo());
        return "details";
    }

    @RequestMapping(value = "/signUpForTournament", method = RequestMethod.GET)
    public String signUpForTournament(Model model, RedirectAttributes redirectAttributes, @RequestParam Long id, Principal principal) {
        TournamentRegistration tournamentAppUser = new TournamentRegistration();
        if (model.containsAttribute("tournamentAppUser")) {
            tournamentAppUser = (TournamentRegistration) model.getAttribute("tournamentAppUser");
        }
        Tournament tournament = tournamentRepository.findById(id).orElse(null);
        if (tournament == null) {
            redirectAttributes.addFlashAttribute("toastMsg", "This tournament does not exist.");
            return "redirect:list";
        }
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getName());
        if (!tournamentService.canApply(tournament, appUser)) {
            redirectAttributes.addFlashAttribute("toastMsg", "You can't register here.");
            return "redirect:list";
        }
        model.addAttribute("tournamentAppUser", tournamentAppUser);
        model.addAttribute("name", tournament.getName());
        return "signUpForTournament";
    }

    @RequestMapping(value = "/signUpForTournament", method = RequestMethod.POST)
    public String signUpForTournament(Model model, RedirectAttributes redirectAttrs, @Valid TournamentRegistration tournamentAppUser, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tournamentAppUser", bindingResult);
            redirectAttrs.addFlashAttribute("tournamentAppUser", tournamentAppUser);
            return "redirect:signUpForTournament?id=" + tournamentAppUser.getTournamentId();
        }

        boolean signedUp = false;
        try {
            signedUp = tournamentService.signUp(tournamentAppUser, userRepository.findByEmailIgnoreCase(principal.getName()));
        } catch (RuntimeException constraintViolation) {
            bindingResult.rejectValue("license", "error.tournamentAppUser", "Enter unique license and ranking");
            redirectAttrs.addFlashAttribute("org.springframework.validation.BindingResult.tournamentAppUser", bindingResult);
            redirectAttrs.addFlashAttribute("tournamentAppUser", tournamentAppUser);
            return "redirect:signUpForTournament?id=" + tournamentAppUser.getTournamentId();
        }
        if (signedUp)
            redirectAttrs.addFlashAttribute("toastMsg", "You've signed up for a tournament");
        else
            redirectAttrs.addFlashAttribute("toastMsg", "Can't sign up for this tournament");
        return "redirect:home";
    }

    @PostMapping("/won")
    public String won(Model model, RedirectAttributes redirectAttrs, @Valid WinInfo winInfo, Principal principal) {
        Match match = matchRepository.getOne(winInfo.getMatchId());
        AppUser user = userRepository.findByEmailIgnoreCase(principal.getName());
        if (!match.hasUser(user)) {
            redirectAttrs.addFlashAttribute("toastMsg", "You can't enter a result for this match.");

            if (winInfo.getTournamentId() == -1) {
                return "redirect:list";
            }
            return "redirect:details?id=" + winInfo.getTournamentId();
        }
        if (!match.isClosed()) {
            tournamentService.tryToUpdateWinners(winInfo, user);
        }
        redirectAttrs.addFlashAttribute("toastMsg", "You've entered the result.");

        if (winInfo.getTournamentId() == -1) {
            return "redirect:list";
        }
        return "redirect:details?id=" + winInfo.getTournamentId();
    }
}
