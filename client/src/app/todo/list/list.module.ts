import {inject, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRouteSnapshot, RouterModule, Routes} from '@angular/router';
import {IonicModule} from '@ionic/angular';
import {ListPage} from './list.page';
import {AuthGuard} from '../../service/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: ListPage,
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)],
    data: {role: 'USER', offline: true},
    pathMatch: 'full'
  },
  {
    path: 'edit',
    loadChildren: () => import('../edit/edit.module').then(m => m.EditPageModule),
    canActivate: [(route: ActivatedRouteSnapshot) => inject(AuthGuard).canActivate(route)],
    data: {role: 'USER', offline: true}
  },
  {
    path: '**',
    redirectTo: '/todos'
  },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [ListPage]
})
export class ListPageModule {
}
