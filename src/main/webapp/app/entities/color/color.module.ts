import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared/shared.module';
import { ColorComponent } from './list/color.component';
import { ColorDetailComponent } from './detail/color-detail.component';
import { ColorUpdateComponent } from './update/color-update.component';
import { ColorDeleteDialogComponent } from './delete/color-delete-dialog.component';
import { ColorRoutingModule } from './route/color-routing.module';

@NgModule({
  imports: [SharedModule, ColorRoutingModule],
  declarations: [ColorComponent, ColorDetailComponent, ColorUpdateComponent, ColorDeleteDialogComponent],
  entryComponents: [ColorDeleteDialogComponent],
})
export class ColorModule {}
