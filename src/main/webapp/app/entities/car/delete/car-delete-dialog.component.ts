import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ICar } from '../car.model';
import { CarService } from '../service/car.service';

@Component({
  templateUrl: './car-delete-dialog.component.html',
})
export class CarDeleteDialogComponent {
  car?: ICar;

  constructor(protected carService: CarService, public activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.carService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
