import { Component, computed } from '@angular/core';
import { httpResource } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { RouterLink } from '@angular/router';
import {
  IonButton,
  IonButtons,
  IonCol,
  IonContent,
  IonFooter,
  IonGrid,
  IonHeader,
  IonMenuButton,
  IonNote,
  IonRouterLink,
  IonRow,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  imports: [
    RouterLink,
    IonRouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonGrid,
    IonRow,
    IonCol,
    IonButton,
    IonButtons,
    IonMenuButton,
    IonFooter,
    IonNote,
  ],
})
export class ProfilePage {
  private readonly serverBuildInfo = httpResource<{ version: string; time: string }>(
    () => '/be/build-info',
  );

  buildInfo = computed(() => ({
    serverVersion: this.serverBuildInfo.value()?.version ?? null,
    serverBuildTime: this.serverBuildInfo.value()?.time ?? null,
    clientVersion: environment.version,
    clientBuildTime: new Date(environment.buildTimestamp * 1000).toISOString(),
  }));
}
