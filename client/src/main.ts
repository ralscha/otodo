import { provideHttpClient, withInterceptors, withXhr } from '@angular/common/http';
import { authenticationInterceptor } from './app/authentication-interceptor';
import {
  PreloadAllModules,
  provideRouter,
  RouteReuseStrategy,
  withHashLocation,
  withPreloading,
} from '@angular/router';
import { ErrorHandler, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { AppGlobalErrorhandler } from './app/app.global.errorhandler';
import { bootstrapApplication, BrowserModule } from '@angular/platform-browser';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment } from './environments/environment';
import { AppComponent } from './app/app.component';
import { IonicRouteStrategy, provideIonicAngular } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import * as useIcons from './use-icons';
import { routes } from './app/app-routing';

addIcons(useIcons);

bootstrapApplication(AppComponent, {
  providers: [
    provideZoneChangeDetection(),
    importProvidersFrom(
      BrowserModule,
      ServiceWorkerModule.register('ngsw-worker.js', { enabled: environment.production }),
    ),
    provideIonicAngular(),
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    { provide: ErrorHandler, useClass: AppGlobalErrorhandler },
    provideHttpClient(withXhr(), withInterceptors([authenticationInterceptor])),
    provideRouter(routes, withHashLocation(), withPreloading(PreloadAllModules)),
  ],
}).catch((err) => console.error(err));
