import {inject} from '@angular/core';
import {ActivatedRouteSnapshot, Routes} from '@angular/router';

import {ListPage} from './list.page';
import {AuthGuard} from '../../service/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: ListPage,
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)],
    data: {role: 'USER', offline: true},
    pathMatch: 'full'
  },
  {
    path: 'edit',
    loadChildren: () => import('../edit/edit.routes').then(m => m.routes),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)],
    data: {role: 'USER', offline: true}
  },
  {
    path: '**',
    redirectTo: '/todos'
  },
];
