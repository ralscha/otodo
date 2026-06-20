import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { ConnectionService } from './connection.service';

export const authGuard: CanActivateFn = (route) => {
  const connectionService = inject(ConnectionService);
  const router = inject(Router);
  const requiredRole = route.data['role'] as 'ADMIN' | 'USER';
  const offlineCapable = route.data['offline'] === true;

  return connectionService.connectionState().pipe(
    take(1),
    map((cs) => {
      if (!cs.isAuthenticated()) {
        return router.createUrlTree([cs.isOnline() ? '/login' : '/offline']);
      }

      if (requiredRole && (cs.isOnline() || offlineCapable)) {
        if (requiredRole === 'ADMIN') {
          return cs.isAdmin();
        }
        return cs.isUser();
      }

      if (cs.isOnline() || offlineCapable) {
        return true;
      }

      if (cs.isUser()) {
        return router.createUrlTree(['/todos']);
      }
      return router.createUrlTree(['/offline']);
    }),
  );
};
