import {Component, OnDestroy, OnInit} from '@angular/core';
import {ConnectionService} from '../service/connection.service';
import {Subscription} from 'rxjs';
import {Router} from '@angular/router';
import {
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonRow,
  IonText,
  IonTitle,
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-offline',
  templateUrl: './offline.page.html',
  styleUrls: ['./offline.page.scss'],
  imports: [
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonGrid,
    IonRow,
    IonCol,
    IonText,
    IonButton
  ]
})
export class OfflinePage implements OnInit, OnDestroy {

  private subscription: Subscription | null = null;

  constructor(private readonly connectionService: ConnectionService,
              private readonly router: Router) {
  }

  reconnect(): void {
    this.connectionService.reconnect();
  }

  ngOnInit(): void {
    this.subscription = this.connectionService.connectionState()
      .subscribe(cs => {
        if (cs.isOnline()) {
          if (cs.isUser()) {
            this.router.navigate(['/todos']);
          } else if (cs.isAdmin()) {
            this.router.navigate(['/users']);
          } else {
            this.router.navigate(['/login']);
          }
        }
      });
  }

  ngOnDestroy(): void {
    if (this.subscription !== null) {
      this.subscription.unsubscribe();
    }
  }

}
