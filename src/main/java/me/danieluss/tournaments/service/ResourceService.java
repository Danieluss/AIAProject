package me.danieluss.tournaments.service;

import me.danieluss.tournaments.data.repo.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceService {

    private final TournamentRepository tournamentRepository;

    @Autowired
    public ResourceService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional
    public byte[] image(Long id, Integer imgNo) {
        return tournamentRepository.getOne(id).getImages().get(imgNo).getBlob();
    }
}
