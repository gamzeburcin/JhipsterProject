import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared/shared.module';
import { RentalComponent } from './list/rental.component';
import { RentalDetailComponent } from './detail/rental-detail.component';
import { RentalUpdateComponent } from './update/rental-update.component';
import { RentalDeleteDialogComponent } from './delete/rental-delete-dialog.component';
import { RentalRoutingModule } from './route/rental-routing.module';

@NgModule({
  imports: [SharedModule, RentalRoutingModule],
  declarations: [RentalComponent, RentalDetailComponent, RentalUpdateComponent, RentalDeleteDialogComponent],
  entryComponents: [RentalDeleteDialogComponent],
})
export class RentalModule {}
