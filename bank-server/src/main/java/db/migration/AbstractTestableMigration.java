package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractTestableMigration extends BaseJavaMigration {

    protected void execute(Context context, String sql, Object...args) throws SQLException {
        sql = String.format(sql, args);
        try (PreparedStatement statement =
                     context
                             .getConnection()
                             .prepareStatement(sql)) {
            statement.execute();
        }
    }

    protected int nextId(Context context) throws SQLException {
        try(Statement statement = context.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery("select nextval('hibernate_sequence')");
            resultSet.next();
            return resultSet.getInt(1);
        }

    }

    static class Tuple {

        Tuple(int id, String consentId) {
            this.id = id;
            this.consentId = consentId;
            this.newConsentId = "urn:raidiambank:" + consentId;
        }

        int id;
        String consentId;
        String newConsentId;
    }

}
