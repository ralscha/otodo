/*
 * This file is generated by jOOQ.
 */
package ch.rasc.otodo.db.tables;


import ch.rasc.otodo.db.DefaultSchema;
import ch.rasc.otodo.db.Indexes;
import ch.rasc.otodo.db.Keys;
import ch.rasc.otodo.db.tables.records.AppUserRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AppUser extends TableImpl<AppUserRecord> {

    private static final long serialVersionUID = -2008689406;

    /**
     * The reference instance of <code>app_user</code>
     */
    public static final AppUser APP_USER = new AppUser();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AppUserRecord> getRecordType() {
        return AppUserRecord.class;
    }

    /**
     * The column <code>app_user.id</code>.
     */
    public final TableField<AppUserRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>app_user.email</code>.
     */
    public final TableField<AppUserRecord, String> EMAIL = createField("email", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>app_user.email_new</code>.
     */
    public final TableField<AppUserRecord, String> EMAIL_NEW = createField("email_new", org.jooq.impl.SQLDataType.VARCHAR(255).defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>app_user.password_hash</code>.
     */
    public final TableField<AppUserRecord, String> PASSWORD_HASH = createField("password_hash", org.jooq.impl.SQLDataType.VARCHAR(255).defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>app_user.authority</code>.
     */
    public final TableField<AppUserRecord, String> AUTHORITY = createField("authority", org.jooq.impl.SQLDataType.VARCHAR(10).nullable(false), this, "");

    /**
     * The column <code>app_user.enabled</code>.
     */
    public final TableField<AppUserRecord, Boolean> ENABLED = createField("enabled", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false), this, "");

    /**
     * The column <code>app_user.expired</code>.
     */
    public final TableField<AppUserRecord, LocalDateTime> EXPIRED = createField("expired", org.jooq.impl.SQLDataType.LOCALDATETIME.defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>app_user.failed_logins</code>.
     */
    public final TableField<AppUserRecord, Integer> FAILED_LOGINS = createField("failed_logins", org.jooq.impl.SQLDataType.INTEGER.defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>app_user.locked_out</code>.
     */
    public final TableField<AppUserRecord, LocalDateTime> LOCKED_OUT = createField("locked_out", org.jooq.impl.SQLDataType.LOCALDATETIME.defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>app_user.last_access</code>.
     */
    public final TableField<AppUserRecord, LocalDateTime> LAST_ACCESS = createField("last_access", org.jooq.impl.SQLDataType.LOCALDATETIME.defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>app_user.confirmation_token</code>.
     */
    public final TableField<AppUserRecord, String> CONFIRMATION_TOKEN = createField("confirmation_token", org.jooq.impl.SQLDataType.CHAR(35).defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.CHAR)), this, "");

    /**
     * The column <code>app_user.confirmation_token_created</code>.
     */
    public final TableField<AppUserRecord, LocalDateTime> CONFIRMATION_TOKEN_CREATED = createField("confirmation_token_created", org.jooq.impl.SQLDataType.LOCALDATETIME.defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>app_user.password_reset_token</code>.
     */
    public final TableField<AppUserRecord, String> PASSWORD_RESET_TOKEN = createField("password_reset_token", org.jooq.impl.SQLDataType.CHAR(35).defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.CHAR)), this, "");

    /**
     * The column <code>app_user.password_reset_token_created</code>.
     */
    public final TableField<AppUserRecord, LocalDateTime> PASSWORD_RESET_TOKEN_CREATED = createField("password_reset_token_created", org.jooq.impl.SQLDataType.LOCALDATETIME.defaultValue(org.jooq.impl.DSL.field("NULL", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * Create a <code>app_user</code> table reference
     */
    public AppUser() {
        this(DSL.name("app_user"), null);
    }

    /**
     * Create an aliased <code>app_user</code> table reference
     */
    public AppUser(String alias) {
        this(DSL.name(alias), APP_USER);
    }

    /**
     * Create an aliased <code>app_user</code> table reference
     */
    public AppUser(Name alias) {
        this(alias, APP_USER);
    }

    private AppUser(Name alias, Table<AppUserRecord> aliased) {
        this(alias, aliased, null);
    }

    private AppUser(Name alias, Table<AppUserRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> AppUser(Table<O> child, ForeignKey<O, AppUserRecord> key) {
        super(child, key, APP_USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.APP_USER_EMAIL, Indexes.APP_USER_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<AppUserRecord, Long> getIdentity() {
        return Keys.IDENTITY_APP_USER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<AppUserRecord> getPrimaryKey() {
        return Keys.KEY_APP_USER_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<AppUserRecord>> getKeys() {
        return Arrays.<UniqueKey<AppUserRecord>>asList(Keys.KEY_APP_USER_PRIMARY, Keys.KEY_APP_USER_EMAIL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUser as(String alias) {
        return new AppUser(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUser as(Name alias) {
        return new AppUser(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AppUser rename(String name) {
        return new AppUser(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AppUser rename(Name name) {
        return new AppUser(name, null);
    }
}