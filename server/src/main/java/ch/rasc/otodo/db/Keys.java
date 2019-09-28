/*
 * This file is generated by jOOQ.
 */
package ch.rasc.otodo.db;


import ch.rasc.otodo.db.tables.AppSession;
import ch.rasc.otodo.db.tables.AppUser;
import ch.rasc.otodo.db.tables.Todo;
import ch.rasc.otodo.db.tables.records.AppSessionRecord;
import ch.rasc.otodo.db.tables.records.AppUserRecord;
import ch.rasc.otodo.db.tables.records.TodoRecord;

import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code></code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<AppUserRecord, Long> IDENTITY_APP_USER = Identities0.IDENTITY_APP_USER;
    public static final Identity<TodoRecord, Long> IDENTITY_TODO = Identities0.IDENTITY_TODO;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AppSessionRecord> KEY_APP_SESSION_PRIMARY = UniqueKeys0.KEY_APP_SESSION_PRIMARY;
    public static final UniqueKey<AppUserRecord> KEY_APP_USER_PRIMARY = UniqueKeys0.KEY_APP_USER_PRIMARY;
    public static final UniqueKey<AppUserRecord> KEY_APP_USER_EMAIL = UniqueKeys0.KEY_APP_USER_EMAIL;
    public static final UniqueKey<TodoRecord> KEY_TODO_PRIMARY = UniqueKeys0.KEY_TODO_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<AppSessionRecord, AppUserRecord> APP_SESSION_IBFK_1 = ForeignKeys0.APP_SESSION_IBFK_1;
    public static final ForeignKey<TodoRecord, AppUserRecord> TODO_IBFK_1 = ForeignKeys0.TODO_IBFK_1;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<AppUserRecord, Long> IDENTITY_APP_USER = Internal.createIdentity(AppUser.APP_USER, AppUser.APP_USER.ID);
        public static Identity<TodoRecord, Long> IDENTITY_TODO = Internal.createIdentity(Todo.TODO, Todo.TODO.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<AppSessionRecord> KEY_APP_SESSION_PRIMARY = Internal.createUniqueKey(AppSession.APP_SESSION, "KEY_app_session_PRIMARY", AppSession.APP_SESSION.ID);
        public static final UniqueKey<AppUserRecord> KEY_APP_USER_PRIMARY = Internal.createUniqueKey(AppUser.APP_USER, "KEY_app_user_PRIMARY", AppUser.APP_USER.ID);
        public static final UniqueKey<AppUserRecord> KEY_APP_USER_EMAIL = Internal.createUniqueKey(AppUser.APP_USER, "KEY_app_user_email", AppUser.APP_USER.EMAIL);
        public static final UniqueKey<TodoRecord> KEY_TODO_PRIMARY = Internal.createUniqueKey(Todo.TODO, "KEY_todo_PRIMARY", Todo.TODO.ID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<AppSessionRecord, AppUserRecord> APP_SESSION_IBFK_1 = Internal.createForeignKey(ch.rasc.otodo.db.Keys.KEY_APP_USER_PRIMARY, AppSession.APP_SESSION, "app_session_ibfk_1", AppSession.APP_SESSION.APP_USER_ID);
        public static final ForeignKey<TodoRecord, AppUserRecord> TODO_IBFK_1 = Internal.createForeignKey(ch.rasc.otodo.db.Keys.KEY_APP_USER_PRIMARY, Todo.TODO, "todo_ibfk_1", Todo.TODO.APP_USER_ID);
    }
}