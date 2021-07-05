import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ICarImage } from '../car-image.model';
import { CarImageService } from '../service/car-image.service';

@Component({
  templateUrl: './car-image-delete-dialog.component.html',
})
export class CarImageDeleteDialogComponent {
  carImage?: ICarImage;

  constructor(protected carImageService: CarImageService, public activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.carImageService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
