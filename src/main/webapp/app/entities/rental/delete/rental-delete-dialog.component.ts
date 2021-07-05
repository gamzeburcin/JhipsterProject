import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IRental } from '../rental.model';
import { RentalService } from '../service/rental.service';

@Component({
  templateUrl: './rental-delete-dialog.component.html',
})
export class RentalDeleteDialogComponent {
  rental?: IRental;

  constructor(protected rentalService: RentalService, public activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.rentalService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
