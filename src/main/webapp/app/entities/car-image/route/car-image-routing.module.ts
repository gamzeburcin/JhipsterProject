import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { CarImageComponent } from '../list/car-image.component';
import { CarImageDetailComponent } from '../detail/car-image-detail.component';
import { CarImageUpdateComponent } from '../update/car-image-update.component';
import { CarImageRoutingResolveService } from './car-image-routing-resolve.service';

const carImageRoute: Routes = [
  {
    path: '',
    component: CarImageComponent,
    data: {
      defaultSort: 'id,asc',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: CarImageDetailComponent,
    resolve: {
      carImage: CarImageRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: CarImageUpdateComponent,
    resolve: {
      carImage: CarImageRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: CarImageUpdateComponent,
    resolve: {
      carImage: CarImageRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(carImageRoute)],
  exports: [RouterModule],
})
export class CarImageRoutingModule {}
