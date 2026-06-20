import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { from, throwError } from 'rxjs';
import { catchError, mergeMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AppDatabase } from './model/app-database';

const ignoreURLs = new Set([
  '/be/login',
  '/be/signup',
  '/be/confirm-signup',
  '/be/reset-password-request',
  '/be/reset-password',
  '/be/confirm-email-change',
]);

export const authenticationInterceptor: HttpInterceptorFn = (request, next) => {
  if (ignoreURLs.has(request.url)) {
    return next(request);
  }

  const appDatabase = inject(AppDatabase);
  const router = inject(Router);

  const tokenGetter = async () => {
    const token = sessionStorage.getItem('token');
    if (token) {
      return token;
    }
    return appDatabase.authenticationToken.limit(1).first();
  };

  return from(tokenGetter()).pipe(
    mergeMap((token: string | undefined) => {
      const authenticatedRequest = token
        ? request.clone({
            setHeaders: {
              'X-authentication': token,
            },
          })
        : request;

      const response$ = next(authenticatedRequest);

      if (request.url.endsWith('/authenticate')) {
        return response$;
      }

      return response$.pipe(
        catchError((err) => {
          if (err.status === 401) {
            appDatabase.authenticationToken.clear();
            router.navigate(['/login']);
          }
          return throwError(() => (err.error && err.error.message) || err.statusText);
        }),
      );
    }),
  );
};
