import { useCallback, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

// Lets AdminLayout (sidebar, logout) know about the form that is currently dirty;
// beforeunload does not fire on in-app router navigation.
let formIsDirty = false;

export function hasUnsavedChanges() {
  return formIsDirty;
}

export function useUnsavedChanges(isDirty: boolean) {
  const { t } = useTranslation();

  useEffect(() => {
    if (!isDirty) return;
    formIsDirty = true;
    const onBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      // Chrome only shows its leave prompt when returnValue is set
      event.returnValue = '';
    };
    window.addEventListener('beforeunload', onBeforeUnload);
    return () => {
      formIsDirty = false;
      window.removeEventListener('beforeunload', onBeforeUnload);
    };
  }, [isDirty]);

  const confirmLeave = useCallback(
    () => !isDirty || window.confirm(t('admin.unsaved.confirmLeave')),
    [isDirty, t]
  );

  return { confirmLeave };
}
