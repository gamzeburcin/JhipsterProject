import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import * as dayjs from 'dayjs';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { IRental, Rental } from '../rental.model';
import { RentalService } from '../service/rental.service';

@Component({
  selector: 'jhi-rental-update',
  templateUrl: './rental-update.component.html',
})
export class RentalUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    rentDate: [],
    returnDate: [],
    customerId: [],
    carId: [],
  });

  constructor(protected rentalService: RentalService, protected activatedRoute: ActivatedRoute, protected fb: FormBuilder) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ rental }) => {
      if (rental.id === undefined) {
        const today = dayjs().startOf('day');
        rental.rentDate = today;
        rental.returnDate = today;
      }

      this.updateForm(rental);
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const rental = this.createFromForm();
    if (rental.id !== undefined) {
      this.subscribeToSaveResponse(this.rentalService.update(rental));
    } else {
      this.subscribeToSaveResponse(this.rentalService.create(rental));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRental>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(rental: IRental): void {
    this.editForm.patchValue({
      id: rental.id,
      rentDate: rental.rentDate ? rental.rentDate.format(DATE_TIME_FORMAT) : null,
      returnDate: rental.returnDate ? rental.returnDate.format(DATE_TIME_FORMAT) : null,
      customerId: rental.customerId,
      carId: rental.carId,
    });
  }

  protected createFromForm(): IRental {
    return {
      ...new Rental(),
      id: this.editForm.get(['id'])!.value,
      rentDate: this.editForm.get(['rentDate'])!.value ? dayjs(this.editForm.get(['rentDate'])!.value, DATE_TIME_FORMAT) : undefined,
      returnDate: this.editForm.get(['returnDate'])!.value ? dayjs(this.editForm.get(['returnDate'])!.value, DATE_TIME_FORMAT) : undefined,
      customerId: this.editForm.get(['customerId'])!.value,
      carId: this.editForm.get(['carId'])!.value,
    };
  }
}
