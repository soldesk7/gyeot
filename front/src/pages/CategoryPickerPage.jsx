import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchGuides } from "../api/guides";
import NonDiagnosticNotice from "../components/NonDiagnosticNotice.jsx";

export default function CategoryPickerPage() {
    const [categories, setCategories] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchGuides()
            .then((data) => setCategories(data))
            .catch((error) => setError(error))
            .finally(() => setLoading(false));
    }, []);

    return (
        <main className="page">
            <h1>증상 직접 선택</h1>

            {loading && (
                <p>증상 목록을 불러오는 중입니다...</p>
            )}
            
            {error && (
                <p>{error.message || "증상 목록을 불러오지 못했어요."}</p>
            )}

            {categories.map((category) => (
                <Link
                    key={category.category}
                    className="action"
                    to={`/guide/${category.category}`}
                >
                    {category.title}
                </Link>
            ))}

            <NonDiagnosticNotice />
            
            <Link className="action" to="/">
                홈으로
            </Link>
        </main>
    );
}