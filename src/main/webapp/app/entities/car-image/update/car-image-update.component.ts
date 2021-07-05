import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import * as dayjs from 'dayjs';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { ICarImage, CarImage } from '../car-image.model';
import { CarImageService } from '../service/car-image.service';

@Component({
  selector: 'jhi-car-image-update',
  templateUrl: './car-image-update.component.html',
})
export class CarImageUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    carId: [],
    imagePath: [],
    date: [],
  });

  constructor(protected carImageService: CarImageService, protected activatedRoute: ActivatedRoute, protected fb: FormBuilder) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ carImage }) => {
      if (carImage.id === undefined) {
        const today = dayjs().startOf('day');
        carImage.date = today;
      }

      this.updateForm(carImage);
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const carImage = this.createFromForm();
    if (carImage.id !== undefined) {
      this.subscribeToSaveResponse(this.carImageService.update(carImage));
    } else {
      this.subscribeToSaveResponse(this.carImageService.create(carImage));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICarImage>>): void {
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

  protected updateForm(carImage: ICarImage): void {
    this.editForm.patchValue({
      id: carImage.id,
      carId: carImage.carId,
      imagePath: carImage.imagePath,
      date: carImage.date ? carImage.date.format(DATE_TIME_FORMAT) : null,
    });
  }

  protected createFromForm(): ICarImage {
    return {
      ...new CarImage(),
      id: this.editForm.get(['id'])!.value,
      carId: this.editForm.get(['carId'])!.value,
      imagePath: this.editForm.get(['imagePath'])!.value,
      date: this.editForm.get(['date'])!.value ? dayjs(this.editForm.get(['date'])!.value, DATE_TIME_FORMAT) : undefined,
    };
  }
}
