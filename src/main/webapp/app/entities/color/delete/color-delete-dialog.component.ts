import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IColor } from '../color.model';
import { ColorService } from '../service/color.service';

@Component({
  templateUrl: './color-delete-dialog.component.html',
})
export class ColorDeleteDialogComponent {
  color?: IColor;

  constructor(protected colorService: ColorService, public activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.colorService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
