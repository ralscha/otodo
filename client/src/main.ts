import {HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {AuthenticationInterceptor} from './app/authentication-interceptor';
import {PreloadAllModules, provideRouter, RouteReuseStrategy, withHashLocation, withPreloading} from '@angular/router';
import {IonicRouteStrategy} from '@ionic/angular';
import {ErrorHandler, importProvidersFrom} from '@angular/core';
import {AppGlobalErrorhandler} from './app/app.global.errorhandler';
import {bootstrapApplication, BrowserModule} from '@angular/platform-browser';
import {ServiceWorkerModule} from '@angular/service-worker';
import {environment} from './environments/environment';
import {AppComponent} from './app/app.component';
import {provideIonicAngular} from "@ionic/angular/standalone";
import {addIcons} from 'ionicons';
import * as useIcons from './use-icons';
import {routes} from "./app/app-routing";

addIcons(useIcons);


bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(BrowserModule, ServiceWorkerModule.register('ngsw-worker.js', {enabled: environment.production})),
    provideIonicAngular(),
    {provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true},
    {provide: RouteReuseStrategy, useClass: IonicRouteStrategy},
    {provide: ErrorHandler, useClass: AppGlobalErrorhandler},
    provideHttpClient(withInterceptorsFromDi()),
    provideRouter(routes, withHashLocation(), withPreloading(PreloadAllModules)),
  ]
})
  .catch(err => console.error(err));
