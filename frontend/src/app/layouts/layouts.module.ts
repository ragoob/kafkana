import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from '../pages/dashboard/dashboard.component';
import { RouterModule, Routes } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SummaryComponent } from '../shared/summary/summary.component';
import { KafkaMonitorService } from '../core/services/kafka-monitor.service';
import { SummaryCardComponent } from '../shared/summary-card/summary-card.component';
import { AccordionModule } from 'primeng/accordion';
import { TableModule } from 'primeng/table';
import { TopicListComponent } from '../shared/topic-list/topic-list.component';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ConsumerListComponent } from '../shared/consumer-list/consumer-list.component';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { KafkaAdminService } from '../core/services/kafka-admin.service';
import { MatTabsModule } from '@angular/material/tabs';
import { BrokersDetailsComponent } from '../shared/brokers-details/brokers-details.component';
import { TopicDetailsComponent } from '../shared/topic-details/topic-details.component';
import { TopicMessagesComponent } from '../shared/topic-messages/topic-messages.component';
import { LoadingService } from '../core/services/loading.service';
import { CalendarModule } from 'primeng/calendar';
import { InputNumberModule } from 'primeng/inputnumber';
import { FormsModule } from '@angular/forms';
import { PayloadFilterComponent } from '../payload-filter/payload-filter.component';
import { MatDialogModule } from '@angular/material/dialog';

 const AdminLayoutRoutes: Routes = [
  { path: 'dashboard/:id', component: DashboardComponent },
  { path: 'brokers-details/:id', component: BrokersDetailsComponent },
  { path: 'topic-details/:id/:topicId', component: TopicDetailsComponent }
];

@NgModule({
  declarations: [
    DashboardComponent,
    SummaryComponent,
    SummaryCardComponent,
    TopicListComponent,
    ConsumerListComponent,
    BrokersDetailsComponent,
    TopicDetailsComponent,
    TopicMessagesComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(AdminLayoutRoutes),
    NgbModule,
    AccordionModule,
    ButtonModule,
    InputTextModule,
    TableModule,
    ConfirmDialogModule,
    MatTabsModule,
    CalendarModule,
    InputNumberModule,
    FormsModule,
    MatDialogModule
    
  ],
  entryComponents: [PayloadFilterComponent],
  providers: [KafkaMonitorService, KafkaAdminService, ConfirmationService, LoadingService]
})
export class LayoutsModule { }
