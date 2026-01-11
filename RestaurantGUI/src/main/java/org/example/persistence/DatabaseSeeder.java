package org.example.persistence;

import org.example.Produs;
import org.example.SampleDataFactory;

import java.util.List;

public class DatabaseSeeder {
    private final ProdusRepository repo;

    public DatabaseSeeder(ProdusRepository repo) {
        this.repo = repo;
    }

    public void seedIfEmpty() {
        if (repo.count() == 0) {
            List<Produs> sample = SampleDataFactory.createSampleProducts();
            for (Produs p : sample) {
                repo.save(ProdusMapper.toEntity(p));
            }
        }

        repo.backfillDessertFlag();
    }
}
