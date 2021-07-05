import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ColorComponent } from '../list/color.component';
import { ColorDetailComponent } from '../detail/color-detail.component';
import { ColorUpdateComponent } from '../update/color-update.component';
import { ColorRoutingResolveService } from './color-routing-resolve.service';

const colorRoute: Routes = [
  {
    path: '',
    component: ColorComponent,
    data: {
      defaultSort: 'id,asc',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ColorDetailComponent,
    resolve: {
      color: ColorRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ColorUpdateComponent,
    resolve: {
      color: ColorRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ColorUpdateComponent,
    resolve: {
      color: ColorRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(colorRoute)],
  exports: [RouterModule],
})
export class ColorRoutingModule {}
