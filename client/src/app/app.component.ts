import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthService} from './service/auth.service';
import {Subscription} from 'rxjs';
import {ConnectionService, ConnectionState} from './service/connection.service';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {
  IonApp,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenu,
  IonMenuToggle,
  IonRouterLink,
  IonRouterOutlet,
  IonSplitPane,
  IonTitle,
  IonToolbar
} from "@ionic/angular/standalone";
import {addIcons} from "ionicons";
import {checkmarkCircleOutline, logOut, people, person} from "ionicons/icons";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [RouterLinkActive, RouterLink, IonRouterLink, IonSplitPane, IonMenu, IonApp, IonContent, IonHeader, IonToolbar, IonTitle, IonList, IonMenuToggle, IonItem, IonIcon, IonLabel, IonRouterOutlet]
})
export class AppComponent implements OnInit, OnDestroy {

  authenticated = false;

  appPages: { title: string, url: string, icon: string }[] = [];

  private subscription!: Subscription;

  constructor(private readonly authService: AuthService,
              private readonly connectionService: ConnectionService) {
    addIcons({checkmarkCircleOutline, people, person, logOut});
  }

  ngOnInit(): void {
    this.subscription = this.connectionService.connectionState().subscribe(cs => this.updateMenu(cs));
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  private updateMenu(connectionState: ConnectionState): void {
    this.authenticated = connectionState.isAuthenticated();

    const newMenu = [];

    if (connectionState.isUser()) {
      newMenu.push({
        title: 'Todos',
        url: '/todos',
        icon: 'checkmark-circle-outline'
      });
    }

    if (connectionState.isAdmin()) {
      newMenu.push({
        title: 'Users',
        url: '/users',
        icon: 'people'
      });
    }

    if (connectionState.isOnlineAuthenticated()) {
      newMenu.push({
        title: 'Profile',
        url: '/profile',
        icon: 'person'
      });
    }

    if (connectionState.isAuthenticated()) {
      newMenu.push({
        title: 'Log out',
        url: '/logout',
        icon: 'log-out'
      });
    }

    this.appPages = newMenu;
  }
}
