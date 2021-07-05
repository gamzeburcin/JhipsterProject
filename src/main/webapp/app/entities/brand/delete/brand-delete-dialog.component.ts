import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IBrand } from '../brand.model';
import { BrandService } from '../service/brand.service';

@Component({
  templateUrl: './brand-delete-dialog.component.html',
})
export class BrandDeleteDialogComponent {
  brand?: IBrand;

  constructor(protected brandService: BrandService, public activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.brandService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}
