import { Routes } from '@angular/router';
import { ListPage } from './list.page';
import { authGuard } from '../../service/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: ListPage,
    canActivate: [authGuard],
    data: { role: 'USER', offline: true },
    pathMatch: 'full',
  },
  {
    path: 'edit',
    loadChildren: () => import('../edit/edit.routes').then((m) => m.routes),
    canActivate: [authGuard],
    data: { role: 'USER', offline: true },
  },
  {
    path: '**',
    redirectTo: '/todos',
  },
];
