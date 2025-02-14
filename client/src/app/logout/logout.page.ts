import {Component, OnInit} from '@angular/core';
import {AuthService} from '../service/auth.service';
import {RouterLink} from '@angular/router';
import {
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonRouterLink,
  IonRow,
  IonText,
  IonTitle,
  IonToolbar
} from "@ionic/angular/standalone";

@Component({
  selector: 'app-logout',
  templateUrl: './logout.page.html',
  styleUrls: ['./logout.page.scss'],
  imports: [RouterLink, IonRouterLink, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonText, IonButton]
})
export class LogoutPage implements OnInit {

  showMsg = false;

  constructor(private readonly authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.logout().subscribe(() => this.showMsg = true);
  }

}
