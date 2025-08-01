import {ErrorHandler, inject, Injectable} from '@angular/core';
import {AppDatabase} from './model/app-database';
import {HttpClient} from '@angular/common/http';
import {ConnectionService} from './service/connection.service';
import {catchError, filter, mapTo, mergeMap, switchMap, take} from 'rxjs/operators';
import {environment} from '../environments/environment';
import {from, iif, noop} from 'rxjs';

@Injectable()
export class AppGlobalErrorhandler implements ErrorHandler {
  private readonly appDatabase = inject(AppDatabase);
  private readonly httpClient = inject(HttpClient);
  private readonly connectionService = inject(ConnectionService);


  constructor() {

    this.connectionService.connectionState()
      .pipe(
        filter(cs => cs.isOnline()),
        switchMap(() => from(this.appDatabase.errors.toArray())),
        filter(errors => errors.length > 0),
        switchMap(errors => this.httpClient.post<void>('/be/client-error', errors).pipe(mapTo(errors))),
        switchMap(errors => {
          const ids: string[] = [];
          for (const error of errors) {
            if (error.id) {
              ids.push("" + error.id);
            }
          }
          return this.appDatabase.errors.bulkDelete(ids);
        })
      )
      .subscribe(noop, noop);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async handleError(error: any): Promise<void> {
    if (!environment.production) {
      console.error(error);
    }

    // @ts-ignore
    const connection = navigator.connection;

    const userAgent = {
      language: navigator.language,
      platform: navigator.platform,
      userAgent: navigator.userAgent,
      connectionType: connection?.type,
    };

    const errorMsg = error && error.message ? error.message : error;
    const body = JSON.stringify({ts: Date.now(), userAgent, error: errorMsg});

    this.connectionService.connectionState().pipe(
      take(1),
      mergeMap(cs => iif(() => cs.isOnline(),
        this.httpClient.post<void>('/be/client-error', [body]),
        this.appDatabase.errors.add({error: body}))
      ),
      catchError(() => this.appDatabase.errors.add({error: body}))
    ).subscribe(noop);
  }

}
