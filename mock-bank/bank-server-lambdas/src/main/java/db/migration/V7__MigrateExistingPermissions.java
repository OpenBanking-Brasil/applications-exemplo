package db.migration;

import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"squid:S2078", "java:S101"})
public class V7__MigrateExistingPermissions extends AbstractTestableMigration {

    private static final Integer CHECKSUM = 4978491;

    @Override
    public void migrate(Context context) throws Exception {

        execute(context, "ALTER TABLE consent_permissions DROP CONSTRAINT consent_permissions_consent_id_fkey");
        execute(context, "ALTER TABLE consent_account_ids DROP CONSTRAINT consent_account_ids_consent_id_fkey");
        int[] ids = setupTestData(context);
        doMigration(context);
        validate(context);
        deleteTestData(context, ids);

    }

    private void doMigration(Context context) throws SQLException {
        migrateTable(context, "SELECT reference_id, consent_id FROM consent_permissions",
                "UPDATE consent_permissions SET new_consent_id=? where reference_id=?");
        migrateTable(context, "SELECT reference_id, consent_id FROM consent_account_ids",
                "UPDATE consent_account_ids SET new_consent_id=? where reference_id=?");
    }

    private void migrateTable(Context context, String selectQuery, String insertQeury) throws SQLException {
        Connection connection = context.getConnection();
        Set<V6__ConsentIdMigration.Tuple> tuples = new HashSet<>();
        try(PreparedStatement statement = connection
                .prepareStatement(selectQuery)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("reference_id");
                String consentId = resultSet.getString("consent_id");
                tuples.add(new V6__ConsentIdMigration.Tuple(id, consentId));
            }
        }

        try(PreparedStatement insert = context.getConnection()
                .prepareStatement(insertQeury)) {
            for(V6__ConsentIdMigration.Tuple tuple: tuples) {
                insert.setInt(2, tuple.id);
                insert.setString(1, tuple.newConsentId);
                insert.addBatch();
            }
            insert.executeBatch();
            connection.commit();
        }

    }

    private void validate(Context context) throws SQLException {
        validateTable(context, "SELECT consent_id, new_consent_id FROM consent_permissions");
        validateTable(context, "SELECT consent_id, new_consent_id FROM consent_account_ids");
    }

    private void validateTable(Context context, String sql) throws SQLException {
        Connection connection = context.getConnection();
        try(PreparedStatement statement = connection
                .prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                String consentId = resultSet.getString("consent_id");
                String newConsentId = resultSet.getString("new_consent_id");
                String expected = "urn:raidiambank:" + consentId;
                if(!newConsentId.equals(expected)) {
                    throw new RuntimeException("Expected the new consent ID to have been populated correctly");
                }
            }
            connection.commit();
        }
    }

    private void deleteTestData(Context context, int[] ids) throws SQLException {
        execute(context, "DELETE FROM consents WHERE reference_id = " + ids[2]);
        execute(context, "DELETE FROM business_entity_documents WHERE business_entity_document_id = " + ids[0]);
        execute(context, "DELETE FROM logged_in_user_entity_documents WHERE logged_in_user_entity_document_id = " + ids[1]);
        execute(context, "DELETE FROM consent_permissions WHERE reference_id = " + ids[3]);
        execute(context, "DELETE FROM consent_account_ids WHERE reference_id = " + ids[4]);
    }

    private int[] setupTestData(Context context) throws SQLException {
        int[] ids = new int[6];
        for(int i = 0 ;i < 6; i++) {
            ids[i] = nextId(context);
        }
        String consentId = UUID.randomUUID().toString();
        String newConsentId = "urn:raidiambank:" + consentId;
        System.out.println("CONSENT ID: " + consentId);

        execute(context,"INSERT INTO business_entity_documents (business_entity_document_id, identification, rel) VALUES (%d, '123', 'abc')", ids[0]);
        execute(context,"INSERT INTO logged_in_user_entity_documents (logged_in_user_entity_document_id, identification, rel) VALUES (%d, '123', 'abc')", ids[1]);
        execute(context,"INSERT INTO consents (reference_id, consent_id, new_consent_id, status_update_date_time, status, business_entity_document_id, logged_in_user_entity_document_id) VALUES (%d, '%s', '%s', now(), 'ONLY_FOR_MIGRATION', %d, %d)", ids[2], consentId, newConsentId, ids[0], ids[1]);

        execute(context,"INSERT INTO consent_permissions (reference_id, permission, consent_id) VALUES (%d, 'ACCOUNTS_READ', '%s')", ids[3], consentId);
        execute(context,"INSERT INTO consent_account_ids (reference_id, account_id, account_type, consent_id) VALUES (%d, '12345', 'ACCOUNT', '%s')", ids[4], consentId);

        return ids;
    }

    @Override
    public Integer getChecksum() {
        return CHECKSUM;
    }
}
