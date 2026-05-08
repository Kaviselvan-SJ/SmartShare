package com.smartshare.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseFixRunner.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Running database fix scripts...");
            // Drop unique constraint on fileHash in files table
            String dropConstraintSql = "DO $$\n" +
                    "DECLARE\n" +
                    "    constraint_name text;\n" +
                    "BEGIN\n" +
                    "    SELECT tc.constraint_name INTO constraint_name\n" +
                    "    FROM information_schema.table_constraints tc\n" +
                    "    JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name\n" +
                    "    WHERE tc.table_name = 'files' AND ccu.column_name = 'file_hash' AND tc.constraint_type = 'UNIQUE';\n" +
                    "    \n" +
                    "    IF constraint_name IS NOT NULL THEN\n" +
                    "        EXECUTE 'ALTER TABLE files DROP CONSTRAINT ' || constraint_name;\n" +
                    "        RAISE NOTICE 'Dropped unique constraint % on files.file_hash', constraint_name;\n" +
                    "    END IF;\n" +
                    "END $$;";
            
            jdbcTemplate.execute(dropConstraintSql);
            logger.info("Database constraint fix executed successfully.");

            // Fix corrupted data: multiple files in the same group having is_current_version = true
            String fixMultipleCurrentVersionsSql = "UPDATE files f1 \n" +
                    "SET is_current_version = false \n" +
                    "WHERE is_current_version = true \n" +
                    "  AND version_number < (\n" +
                    "      SELECT MAX(version_number) \n" +
                    "      FROM files f2 \n" +
                    "      WHERE f2.file_group_id = f1.file_group_id\n" +
                    "  )";
            int updatedRows = jdbcTemplate.update(fixMultipleCurrentVersionsSql);
            logger.info("Fixed {} corrupted files with multiple current versions in the same group.", updatedRows);

        } catch (Exception e) {
            logger.error("Error executing database fix script: ", e);
        }
    }
}
