--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE carsharing_db;
--
-- Name: carsharing_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE carsharing_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';


ALTER DATABASE carsharing_db OWNER TO postgres;

\connect carsharing_db

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: car_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.car_type AS ENUM (
    'base',
    'comfort',
    'business'
);


ALTER TYPE public.car_type OWNER TO postgres;

--
-- Name: function_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.function_type AS ENUM (
    'child_chair',
    'transponder'
);


ALTER TYPE public.function_type OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: function; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.function (
    id integer NOT NULL,
    transport_id integer NOT NULL,
    function_data public.function_type NOT NULL
);


ALTER TABLE public.function OWNER TO postgres;

--
-- Name: function_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.function_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.function_id_seq OWNER TO postgres;

--
-- Name: function_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.function_id_seq OWNED BY public.function.id;


--
-- Name: rate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rate (
    id integer NOT NULL,
    transport_id integer NOT NULL,
    rate_name text NOT NULL,
    on_road_price real NOT NULL,
    parking_price real NOT NULL
);


ALTER TABLE public.rate OWNER TO postgres;

--
-- Name: rate_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rate_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rate_id_seq OWNER TO postgres;

--
-- Name: rate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rate_id_seq OWNED BY public.rate.id;


--
-- Name: transport; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transport (
    id integer NOT NULL,
    transport_type public.car_type NOT NULL,
    car_name text NOT NULL,
    car_number character varying(9) NOT NULL,
    car_image_path text NOT NULL,
    is_reserved boolean DEFAULT false NOT NULL,
    gas_level smallint NOT NULL,
    insurance_type text NOT NULL,
    is_door_opened boolean NOT NULL,
    longitude real NOT NULL,
    latitude real NOT NULL,
    gas_consumption smallint,
    tank_capacity smallint
);


ALTER TABLE public.transport OWNER TO postgres;

--
-- Name: transport_full_info; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.transport_full_info AS
 SELECT t.id,
    t.transport_type,
    t.car_name,
    t.car_number,
    t.car_image_path,
    t.is_reserved,
    t.gas_level,
    t.insurance_type,
    t.is_door_opened,
    t.longitude,
    t.latitude,
    t.gas_consumption,
    t.tank_capacity,
    f.function_data
   FROM (public.transport t
     JOIN public.function f ON ((t.id = f.transport_id)));


ALTER VIEW public.transport_full_info OWNER TO postgres;

--
-- Name: transport_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transport_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transport_id_seq OWNER TO postgres;

--
-- Name: transport_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.transport_id_seq OWNED BY public.transport.id;


--
-- Name: function id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.function ALTER COLUMN id SET DEFAULT nextval('public.function_id_seq'::regclass);


--
-- Name: rate id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rate ALTER COLUMN id SET DEFAULT nextval('public.rate_id_seq'::regclass);


--
-- Name: transport id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transport ALTER COLUMN id SET DEFAULT nextval('public.transport_id_seq'::regclass);


--
-- Data for Name: function; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.function (id, transport_id, function_data) FROM stdin;
1	1	child_chair
2	1	transponder
\.


--
-- Data for Name: rate; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rate (id, transport_id, rate_name, on_road_price, parking_price) FROM stdin;
1	1	Фикс	5	5
2	1	Поминутно	12.34	4.56
\.


--
-- Data for Name: transport; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.transport (id, transport_type, car_name, car_number, car_image_path, is_reserved, gas_level, insurance_type, is_door_opened, longitude, latitude, gas_consumption, tank_capacity) FROM stdin;
1	base	Daewoo Nexia	К742СМ53	nexia.png	f	45	ОСАГО	f	31.32089	58.56117	8	50
2	base	Volkswagen Golf IV	Е756ЕВ53	volkswagen_golf_4.png	f	34	ОСАГО	f	31.315336	58.562798	8	55
\.


--
-- Name: function_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.function_id_seq', 2, true);


--
-- Name: rate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rate_id_seq', 2, true);


--
-- Name: transport_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.transport_id_seq', 5, true);


--
-- Name: function function_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.function
    ADD CONSTRAINT function_pkey PRIMARY KEY (id);


--
-- Name: rate rate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_pkey PRIMARY KEY (id);


--
-- Name: transport transport_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transport
    ADD CONSTRAINT transport_pkey PRIMARY KEY (id);


--
-- Name: function function_transport_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.function
    ADD CONSTRAINT function_transport_id_fkey FOREIGN KEY (transport_id) REFERENCES public.transport(id);


--
-- Name: rate rate_transport_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_transport_id_fkey FOREIGN KEY (transport_id) REFERENCES public.transport(id);


--
-- PostgreSQL database dump complete
--