package db.migration;

import static ch.rasc.otodo.db.tables.AppUser.APP_USER;
import static org.jooq.impl.DSL.using;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class V0002__initial_import extends BaseJavaMigration {

  @Override
  public void migrate(Context context) {

    PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    try (DSLContext dsl = using(context.getConnection())) {

      dsl.transaction(txConf -> {
        try (var txdsl = DSL.using(txConf)) {
          try {
			txdsl
			      .insertInto(APP_USER, APP_USER.EMAIL, APP_USER.PASSWORD_HASH,
			          APP_USER.AUTHORITY, APP_USER.ENABLED, APP_USER.EXPIRED,
			          APP_USER.FAILED_LOGINS, APP_USER.LOCKED_OUT, APP_USER.LAST_ACCESS,
			          APP_USER.CONFIRMATION_TOKEN, APP_USER.CONFIRMATION_TOKEN_CREATED,
			          APP_USER.PASSWORD_RESET_TOKEN, APP_USER.PASSWORD_RESET_TOKEN_CREATED)
			      .values("admin@test.com", pe.encode("password"), "ADMIN", true, null, null,
			          null, null, null, null, null, null)
			      .values("user@test.com", pe.encode("password"), "USER", true, null, null,
			          null, null, null, null, null, null)
			      .execute();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
      });
    }

  }
}