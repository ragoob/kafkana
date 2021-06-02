import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AdminLayoutComponent } from './layouts/admin-layout.component';

export const AppRoutes: Routes = [
  {
    path: '',
    component: HomeComponent,
    pathMatch: 'full',
  }, {
    path: '',
    component: AdminLayoutComponent,
    children: [
      {
        path: '',
        loadChildren: () => import(`./layouts/layouts.module`).then(
          module => module.LayoutsModule
        )
      }]
  },
  {
    path: '**',
    redirectTo: ''
  }
]
@NgModule({
  imports: [RouterModule.forRoot(AppRoutes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
