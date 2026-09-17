import { useState, type FormEvent } from 'react'
import { categoryApi } from '@/api/categoryApi'
import { Modal } from '@/components/common/Modal'
import { useCategories } from '@/hooks/useCategories'
import { useToast } from '@/hooks/useToast'
import './CategoryManageModal.css'

export function CategoryManageModal({ onClose }: { onClose: () => void }) {
  const { categories, loading, refresh } = useCategories()
  const { showToast } = useToast()
  const [newName, setNewName] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const run = async (action: () => Promise<void>, message: string) => {
    setSubmitting(true)
    try {
      await action()
      await refresh()
      showToast(message, 'success')
    } catch (err) {
      showToast((err as Error).message, 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault()
    const name = newName.trim()
    if (!name) return

    await run(() => categoryApi.createCategory(name), '카테고리를 추가했습니다.')
    setNewName('')
  }

  const beginEdit = (categoryId: number, name: string) => {
    setEditingId(categoryId)
    setEditingName(name)
  }

  const handleUpdate = async (categoryId: number) => {
    const newName = editingName.trim()
    if (!newName) return

    await run(() => categoryApi.updateCategory(categoryId, newName), '카테고리명을 수정했습니다.')
    setEditingId(null)
    setEditingName('')
  }

  const handleDelete = async (categoryId: number, name: string) => {
    if (!window.confirm(`'${name}' 카테고리를 삭제할까요?`)) return
    await run(() => categoryApi.deleteCategory(categoryId), '카테고리를 삭제했습니다.')
  }

  return (
    <Modal title="카테고리 관리" onClose={onClose}>
      <form className="category-manage__create" onSubmit={handleCreate}>
        <input
          className="input"
          value={newName}
          onChange={(event) => setNewName(event.target.value)}
          placeholder="새 카테고리명"
          aria-label="새 카테고리명"
          disabled={submitting}
        />
        <button type="submit" className="btn btn--primary btn--sm" disabled={submitting || !newName.trim()}>
          추가
        </button>
      </form>

      {loading ? (
        <p className="category-manage__empty">카테고리를 불러오는 중입니다.</p>
      ) : categories.length === 0 ? (
        <p className="category-manage__empty">등록된 카테고리가 없습니다.</p>
      ) : (
        <ul className="category-manage__list">
          {categories.map((category) => (
            <li key={category.id} className="category-manage__item">
              {editingId === category.id ? (
                <input
                  className="input category-manage__edit-input"
                  value={editingName}
                  onChange={(event) => setEditingName(event.target.value)}
                  aria-label={`${category.name} 카테고리명 수정`}
                  disabled={submitting}
                />
              ) : (
                <span>{category.name}</span>
              )}

              <div className="category-manage__actions">
                {editingId === category.id ? (
                  <>
                    <button
                      type="button"
                      className="btn btn--primary btn--sm"
                      onClick={() => void handleUpdate(category.id)}
                      disabled={submitting || !editingName.trim()}
                    >
                      저장
                    </button>
                    <button
                      type="button"
                      className="btn btn--ghost btn--sm"
                      onClick={() => setEditingId(null)}
                      disabled={submitting}
                    >
                      취소
                    </button>
                  </>
                ) : (
                  <>
                    <button
                      type="button"
                      className="btn btn--ghost btn--sm"
                      onClick={() => beginEdit(category.id, category.name)}
                      disabled={submitting}
                    >
                      수정
                    </button>
                    <button
                      type="button"
                      className="btn btn--ghost btn--sm"
                      onClick={() => void handleDelete(category.id, category.name)}
                      disabled={submitting}
                    >
                      삭제
                    </button>
                  </>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
      <p className="category-manage__guide">상품이 연결된 카테고리는 삭제할 수 없습니다.</p>
    </Modal>
  )
}
