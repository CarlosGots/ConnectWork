import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div style="font-family: Arial; padding: 40px; max-width: 600px; margin: 0 auto;">
      <h1 style="color: #2E4057;">ConnectWork Frontend</h1>
      <p>Frontend Angular corriendo correctamente.</p>

      @if (estadoBackend() === 'cargando') {
        <div style="padding: 20px; background: #fff3cd; border-radius: 8px;">
          ⏳ Conectando al backend...
        </div>
      } @else if (estadoBackend() === 'ok') {
        <div style="padding: 20px; background: #e8f5e9; color: #2e7d32; border-radius: 8px;">
          ✓ Backend conectado correctamente<br>
          <small>{{ mensajeBackend() }}</small>
        </div>
      } @else {
        <div style="padding: 20px; background: #ffebee; color: #c62828; border-radius: 8px;">
          ✗ Error al conectar al backend<br>
          <small>{{ mensajeBackend() }}</small>
        </div>
      }

      <router-outlet />
    </div>
  `,
})
export class App implements OnInit {
  private http = inject(HttpClient);

  estadoBackend = signal<'cargando' | 'ok' | 'error'>('cargando');
  mensajeBackend = signal<string>('');

  ngOnInit() {
    this.http.get('http://localhost:8080/connectwork-backend/test', { responseType: 'text' })
      .subscribe({
        next: (respuesta) => {
          this.estadoBackend.set('ok');
          this.mensajeBackend.set('Respuesta recibida del servlet de prueba');
          console.log('Respuesta del backend:', respuesta);
        },
        error: (err) => {
          this.estadoBackend.set('error');
          this.mensajeBackend.set(err.message || 'Error desconocido');
          console.error('Error CORS o conexión:', err);
        }
      });
  }
}