import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { fetchGuides } from "../api/guides";
import NonDiagnosticNotice from "../components/NonDiagnosticNotice.jsx";

export default function CategoryPickerPage() {
    const [categories, setCategories] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const location = useLocation();
    const lowConfidence = location.state?.lowConfidence === true;

    useEffect(() => {
        fetchGuides()
            .then((data) => setCategories(data))
            .catch((error) => setError(error))
            .finally(() => setLoading(false));
    }, []);

    return (
        <main className="page">
            <h1>증상 직접 선택</h1>

            {lowConfidence && (
                <p>
                    사진만으로 상황을 확실하게 판단하기 어려워요. 직접 상황을 선택해주세요.
                </p>
            )}

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