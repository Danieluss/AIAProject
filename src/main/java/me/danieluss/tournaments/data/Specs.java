package me.danieluss.tournaments.data;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Specs<T> {

    public Specification<T> search(String text) {
        String finalText = "%" + text + "%";
        return (root, query, builder) -> {
            List<Predicate> predicates = root.getModel()
                    .getSingularAttributes()
                    .stream()
                    .filter(a -> a.getType().getJavaType().getAnnotation(Entity.class) == null)
                    .map(a -> builder.like(builder.lower(root.get(a.getName()).as(String.class)), finalText.toLowerCase()))
                    .collect(Collectors.toList());
            return builder.or(predicates.toArray(Predicate[]::new));
        };
    }

}
