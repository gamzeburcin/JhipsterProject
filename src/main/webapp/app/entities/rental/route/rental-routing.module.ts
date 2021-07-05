import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { RentalComponent } from '../list/rental.component';
import { RentalDetailComponent } from '../detail/rental-detail.component';
import { RentalUpdateComponent } from '../update/rental-update.component';
import { RentalRoutingResolveService } from './rental-routing-resolve.service';

const rentalRoute: Routes = [
  {
    path: '',
    component: RentalComponent,
    data: {
      defaultSort: 'id,asc',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: RentalDetailComponent,
    resolve: {
      rental: RentalRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: RentalUpdateComponent,
    resolve: {
      rental: RentalRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: RentalUpdateComponent,
    resolve: {
      rental: RentalRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(rentalRoute)],
  exports: [RouterModule],
})
export class RentalRoutingModule {}
