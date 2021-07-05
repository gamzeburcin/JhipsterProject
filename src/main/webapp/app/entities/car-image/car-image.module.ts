import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared/shared.module';
import { CarImageComponent } from './list/car-image.component';
import { CarImageDetailComponent } from './detail/car-image-detail.component';
import { CarImageUpdateComponent } from './update/car-image-update.component';
import { CarImageDeleteDialogComponent } from './delete/car-image-delete-dialog.component';
import { CarImageRoutingModule } from './route/car-image-routing.module';

@NgModule({
  imports: [SharedModule, CarImageRoutingModule],
  declarations: [CarImageComponent, CarImageDetailComponent, CarImageUpdateComponent, CarImageDeleteDialogComponent],
  entryComponents: [CarImageDeleteDialogComponent],
})
export class CarImageModule {}
