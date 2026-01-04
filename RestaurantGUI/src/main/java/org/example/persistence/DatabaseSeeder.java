package org.example.persistence;

import org.example.Produs;
import org.example.SampleDataFactory;

import java.util.List;

/** Inserts initial sample data into DB if empty. */
public class DatabaseSeeder {
    private final ProdusRepository repo;

    public DatabaseSeeder(ProdusRepository repo) {
        this.repo = repo;
    }

    public void seedIfEmpty() {
        // If DB is empty, insert sample data.
        if (repo.count() == 0) {
            List<Produs> sample = SampleDataFactory.createSampleProducts();
            for (Produs p : sample) {
                repo.save(ProdusMapper.toEntity(p));
            }
        }

        // Backfill new columns / fix legacy data.
        repo.backfillDessertFlag();
    }
}
