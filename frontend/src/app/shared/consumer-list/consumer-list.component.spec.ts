import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsumerListComponent } from './consumer-list.component';

describe('ConsumerListComponent', () => {
  let component: ConsumerListComponent;
  let fixture: ComponentFixture<ConsumerListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConsumerListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsumerListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
