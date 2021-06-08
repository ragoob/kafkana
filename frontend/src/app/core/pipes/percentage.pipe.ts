import { PipeTransform, Pipe } from '@angular/core';

@Pipe({ name: 'percentage' })
export class PercentagePipe implements PipeTransform {
    transform(value?: number): string {
        if (value) {
            return (value * 100).toFixed(0) + ' %'
        }
        return 'None';
    }
}