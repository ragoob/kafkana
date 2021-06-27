import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { environment } from '../environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutModule } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { RouterModule } from '@angular/router';
import { API_BASE_URL } from './core/constants';
import { AdminLayoutComponent } from './layouts/admin-layout.component';
import { HomeComponent } from './home/home.component';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { AdminService } from './core/services/admin.service';
import { MatDialogModule, MAT_DIALOG_DEFAULT_OPTIONS } from '@angular/material/dialog';
import { AddNewComponent } from './add-new/add-new.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SideBarComponent } from './side-bar/side-bar.component';
import { NavComponent } from './nav/nav.component';
import { PayloadFilterComponent } from './payload-filter/payload-filter.component';
import { ClusterComponent } from './cluster/cluster.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { AddNewClusterButtonComponent } from './add-new-cluster-button/add-new-cluster-button.component';
import { TopicCreateComponent } from './shared/topic-create/topic-create.component';
import { ActionNotificationComponent } from './action-natification/action-notification.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { LayoutUtilsService } from './core/services/layout-utils.service';
import { MapControlComponent } from './map-control/map-control.component';
import { ConfirmDeleteDialogComponent } from './confirm-delete-dialog/confirm-delete-dialog.component';
import { MatProgressBarModule } from '@angular/material/progress-bar';



@NgModule({
  declarations: [
    AppComponent,
    AdminLayoutComponent,
    HomeComponent,
    AddNewComponent,
    SideBarComponent,
    NavComponent,
    PayloadFilterComponent,
    ClusterComponent,
    AddNewClusterButtonComponent,
    TopicCreateComponent,
    ActionNotificationComponent,
    MapControlComponent,
    ConfirmDeleteDialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    LayoutModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MatGridListModule,
    MatCardModule,
    MatMenuModule,
    RouterModule,
    ButtonModule,
    InputTextModule,
    TableModule,
    MatDialogModule,
    FormsModule,
    ReactiveFormsModule,
    DragDropModule,
    MatSnackBarModule,
    MatProgressBarModule
    

  ],
  providers: [{ provide: API_BASE_URL, useValue: environment.baseURL }, LayoutUtilsService, AdminService, { provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: { hasBackdrop: true, direction: 'ltr' } }],
  entryComponents: [AddNewComponent, PayloadFilterComponent],
  bootstrap: [AppComponent]
})
export class AppModule { }
