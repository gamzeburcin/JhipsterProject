import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'brand',
        data: { pageTitle: 'rentalCarApp.brand.home.title' },
        loadChildren: () => import('./brand/brand.module').then(m => m.BrandModule),
      },
      {
        path: 'car-image',
        data: { pageTitle: 'rentalCarApp.carImage.home.title' },
        loadChildren: () => import('./car-image/car-image.module').then(m => m.CarImageModule),
      },
      {
        path: 'color',
        data: { pageTitle: 'rentalCarApp.color.home.title' },
        loadChildren: () => import('./color/color.module').then(m => m.ColorModule),
      },
      {
        path: 'customer',
        data: { pageTitle: 'rentalCarApp.customer.home.title' },
        loadChildren: () => import('./customer/customer.module').then(m => m.CustomerModule),
      },
      {
        path: 'payment',
        data: { pageTitle: 'rentalCarApp.payment.home.title' },
        loadChildren: () => import('./payment/payment.module').then(m => m.PaymentModule),
      },
      {
        path: 'rental',
        data: { pageTitle: 'rentalCarApp.rental.home.title' },
        loadChildren: () => import('./rental/rental.module').then(m => m.RentalModule),
      },
      {
        path: 'car',
        data: { pageTitle: 'rentalCarApp.car.home.title' },
        loadChildren: () => import('./car/car.module').then(m => m.CarModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
