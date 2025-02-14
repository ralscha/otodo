import {ActivatedRouteSnapshot, Routes} from '@angular/router';
import {ProfilePage} from './profile.page';
import {AuthGuard} from "../../service/auth.guard";
import {inject} from "@angular/core";

export const routes: Routes = [
  {
    path: '',
    component: ProfilePage,
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)],
    pathMatch: 'full'
  },
  {
    path: 'password',
    loadComponent: () => import('../password/password.page').then(m => m.PasswordPage),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)]
  },
  {
    path: 'email',
    loadComponent: () => import('../email/email.page').then(m => m.EmailPage),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)]
  },
  {
    path: 'sessions',
    loadComponent: () => import('../sessions/sessions.page').then(m => m.SessionsPage),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)]
  },
  {
    path: 'account',
    loadComponent: () => import('../account/account.page').then(m => m.AccountPage),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)]
  },
  {
    path: '**',
    redirectTo: '/profile'
  }
];
