import {Routes} from '@angular/router';

import {EditPage} from './edit.page';

export const routes: Routes = [
  {
    path: ':id',
    component: EditPage
  },
  {
    path: '',
    component: EditPage
  }
];
