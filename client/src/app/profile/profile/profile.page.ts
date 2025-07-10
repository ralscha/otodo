import {Component, inject, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {RouterLink} from '@angular/router';
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
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  imports: [RouterLink, IonRouterLink, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonButton, IonButtons, IonMenuButton, IonFooter, IonNote]
})
export class ProfilePage implements OnInit {
  buildInfo: {
    serverVersion: string | null,
    serverBuildTime: string | null,
    clientVersion: string | null,
    clientBuildTime: string | null
  } = {serverVersion: null, serverBuildTime: null, clientVersion: null, clientBuildTime: null};
  private readonly httpClient = inject(HttpClient);

  ngOnInit(): void {
    this.buildInfo.clientVersion = environment.version;
    this.buildInfo.clientBuildTime = new Date(environment.buildTimestamp * 1000).toISOString();

    this.httpClient.get<{ version: string, time: string }>('/be/build-info')
      .subscribe(response => {
        this.buildInfo.serverVersion = response.version;
        this.buildInfo.serverBuildTime = response.time;
      });
  }

}
