(() => {
	'use strict';

	const year = document.querySelector('[data-current-year]');
	if (year) year.textContent = String(new Date().getFullYear());

	const healthIndicator = document.querySelector('[data-health-indicator]');
	const healthLabel = document.querySelector('[data-health-label]');
	if (healthIndicator && healthLabel) {
		Promise.all([
			fetch('/actuator/health', { headers: { Accept: 'application/json' } }),
			fetch('/actuator/metrics', { headers: { Accept: 'application/json' } })
		]).then(([health, metrics]) => {
			if (!health.ok) throw new Error('The system health endpoint is unavailable.');
			return health.json().then(({ status }) => ({
				status,
				metricsAvailable: metrics.ok && !metrics.redirected
					&& metrics.headers.get('content-type')?.includes('application/json')
			}));
		}).then(({ status, metricsAvailable }) => {
			const online = status === 'UP';
			healthIndicator.classList.add(online ? 'is-online' : 'is-offline');
			healthLabel.textContent = online
				? (metricsAvailable ? 'Systems healthy · metrics active' : 'System healthy')
				: 'System attention needed';
		}).catch(() => {
			healthIndicator.classList.add('is-offline');
			healthLabel.textContent = 'Health check unavailable';
		});
	}

	document.querySelectorAll('[data-confirm]').forEach((form) => {
		form.addEventListener('submit', (event) => {
			if (!window.confirm(form.dataset.confirm)) event.preventDefault();
		});
	});

	const amountInput = document.querySelector('[data-money-input]');
	if (amountInput instanceof HTMLInputElement) {
		amountInput.addEventListener('input', () => {
			const raw = amountInput.value.replace(/[^\d.,]/g, '');
			const normalized = raw.includes(',') && !raw.includes('.')
				? raw.replace(',', '.')
				: raw.replace(/,/g, '');
			const [whole = '', ...fractionParts] = normalized.split('.');
			const fraction = fractionParts.join('').slice(0, 2);
			amountInput.value = normalized.includes('.') ? `${whole}.${fraction}` : whole;
			amountInput.setCustomValidity('');
		});
		amountInput.addEventListener('blur', () => {
			if (!amountInput.value) return;
			const amount = Number(amountInput.value);
			if (!Number.isFinite(amount) || amount < 0.01) {
				amountInput.setCustomValidity('Enter an amount of at least $0.01.');
				return;
			}
			amountInput.setCustomValidity('');
			amountInput.value = amount.toFixed(2);
		});
	}

	const expenseForm = document.querySelector('.expense-form');
	const validationMessage = document.querySelector('[data-form-message]');
	if (expenseForm instanceof HTMLFormElement && validationMessage instanceof HTMLElement) {
		expenseForm.addEventListener('submit', (event) => {
			if (!expenseForm.checkValidity()) {
				event.preventDefault();
				validationMessage.textContent = 'Please complete each field with a valid value.';
				expenseForm.reportValidity();
			} else {
				validationMessage.textContent = '';
			}
		});
	}
})();
