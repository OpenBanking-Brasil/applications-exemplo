package db.migration;

import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"squid:S2078", "java:S101"})
public class V6__ConsentIdMigration extends AbstractTestableMigration {

    private static final Integer CHECKSUM = 64984658;

    @Override
    public void migrate(Context context) throws Exception {
        int[] ids = setupTestData(context);
        doMigration(context);
        validate(context);
        deleteTestData(context, ids);
    }

    private void doMigration(Context context) throws SQLException {
        Connection connection = context.getConnection();
        Set<Tuple> tuples = new HashSet<>();
        try(PreparedStatement statement = connection
                .prepareStatement("SELECT reference_id, consent_id FROM consents")) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("reference_id");
                String consentId = resultSet.getString("consent_id");
                tuples.add(new Tuple(id, consentId));
            }
        }

        try(PreparedStatement insert = context.getConnection()
                .prepareStatement("UPDATE consents SET new_consent_id=? where reference_id=?")) {
            for(Tuple tuple: tuples) {
                insert.setInt(2, tuple.id);
                insert.setString(1, tuple.newConsentId);
                insert.addBatch();
            }
            insert.executeBatch();
            connection.commit();
        }

    }

    private void validate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        try(PreparedStatement statement = connection
                .prepareStatement("SELECT consent_id, new_consent_id FROM consents")) {
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

    private int[] setupTestData(Context context) throws SQLException {

        int[] ids = new int[6];
        for(int i = 0 ;i < 6; i++) {
            ids[i] = nextId(context);
        }

        execute(context,"INSERT INTO business_entity_documents (business_entity_document_id, identification, rel) VALUES (%d, '123', 'abc')", ids[0]);
        execute(context,"INSERT INTO logged_in_user_entity_documents (logged_in_user_entity_document_id, identification, rel) VALUES (%d, '123', 'abc')", ids[1]);
        execute(context,"INSERT INTO consents (reference_id,status_update_date_time, status, business_entity_document_id, logged_in_user_entity_document_id) VALUES (%d, now(), 'ONLY_FOR_MIGRATION', %d, %d)", ids[2], ids[0], ids[1]);

        execute(context,"INSERT INTO business_entity_documents (business_entity_document_id, identification, rel) VALUES (%d, '123', 'abc')", ids[3]);
        execute(context,"INSERT INTO logged_in_user_entity_documents (logged_in_user_entity_document_id, identification, rel) VALUES (%d, '123', 'abc')", ids[4]);
        execute(context,"INSERT INTO consents (reference_id,status_update_date_time, status, business_entity_document_id, logged_in_user_entity_document_id) VALUES (%d, now(), 'ONLY_FOR_MIGRATION', %d, %d)", ids[5], ids[3], ids[4]);

        return ids;

    }

    private void deleteTestData(Context context, int[] ids) throws SQLException {
        execute(context, "DELETE FROM consents WHERE reference_id = " + ids[2]);
        execute(context, "DELETE FROM consents WHERE reference_id = " + ids[5]);
        execute(context, "DELETE FROM business_entity_documents WHERE business_entity_document_id = " + ids[0]);
        execute(context, "DELETE FROM logged_in_user_entity_documents WHERE logged_in_user_entity_document_id = " + ids[1]);
        execute(context, "DELETE FROM business_entity_documents WHERE business_entity_document_id = " + ids[3]);
        execute(context, "DELETE FROM logged_in_user_entity_documents WHERE logged_in_user_entity_document_id = " + ids[4]);
    }

    @Override
    public Integer getChecksum() {
        return CHECKSUM;
    }

}
