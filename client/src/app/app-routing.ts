import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Routes} from '@angular/router';
import {AuthGuard} from './service/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/todos',
    pathMatch: 'full'
  },
  {
    path: 'todos',
    loadChildren: () => import('./todo/list/list.routes').then(m => m.routes),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)],
    data: {role: 'USER', offline: true}
  },
  {
    path: 'profile',
    loadChildren: () => import('./profile/profile/profile.routes').then(m => m.routes),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)]
  },
  {
    path: 'email-change-confirm',
    loadChildren: () => import('./profile/email-change-confirm/email-change-confirm.routes').then(m => m.routes)
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.page').then(m => m.LoginPage)
  },
  {
    path: 'logout',
    loadComponent: () => import('./logout/logout.page').then(m => m.LogoutPage)
  },
  {
    path: 'signup',
    loadComponent: () => import('./signup/signup.page').then(m => m.SignupPage)
  },
  {
    path: 'signup-confirm',
    loadChildren: () => import('./signup-confirm/signup-confirm.routes').then(m => m.routes)
  },
  {
    path: 'password-reset-request',
    loadChildren: () => import('./password-reset-request/password-reset-request.page').then(m => m.PasswordResetRequestPage)
  },
  {
    path: 'password-reset',
    loadChildren: () => import('./password-reset/password-reset.routes').then(m => m.routes)
  },
  {
    path: 'users',
    loadComponent: () => import('./users/users.page').then(m => m.UsersPage),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)],
    data: {role: 'ADMIN'}
  },
  {
    path: 'offline',
    loadComponent: () => import('./offline/offline.page').then(m => m.OfflinePage)
  },
  {
    path: '**',
    redirectTo: '/todos'
  }

];
