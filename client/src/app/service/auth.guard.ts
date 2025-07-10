import {inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Router, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from './auth.service';
import {map, take} from 'rxjs/operators';
import {ConnectionService} from './connection.service';

@Injectable(
  {providedIn: 'root'}
)
export class AuthGuard {
  private readonly authService = inject(AuthService);
  private readonly connectionService = inject(ConnectionService);
  private readonly router = inject(Router);


  canActivate(next: ActivatedRouteSnapshot):
    Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const requiredRole = next.data['role'] as 'ADMIN' | 'USER';
    const offlineCapable = next.data['offline'] === true;

    return this.connectionService.connectionState()
      .pipe(
        take(1),
        map(cs => {
          if (!cs.isAuthenticated()) {
            if (cs.isOnline()) {
              return this.router.createUrlTree(['/login']);
            }
            return this.router.createUrlTree(['/offline']);
          }

          if (requiredRole && (cs.isOnline() || offlineCapable)) {
            if (requiredRole === 'ADMIN' && cs.isAdmin()) {
              return true;
            } else {
              return requiredRole === 'USER' && cs.isUser();
            }
          } else if (cs.isOnline() || offlineCapable) {
            return true;
          } else {
            if (cs.isUser()) {
              return this.router.createUrlTree(['/todos']);
            }
            return this.router.createUrlTree(['/offline']);
          }
        })
      );
  }

}
